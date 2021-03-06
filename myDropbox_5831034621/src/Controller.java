import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import schemas.FileInfoSchema;
import schemas.UserInfoSchema;

public class Controller {

    private static DynamoDBMapper mapper;
    private static MyS3Wrapper s3;
    public static String bucketPrefix = "mydropbox10346-";

    public Controller(String region){
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder
                .standard()
                .withCredentials(new ProfileCredentialsProvider())
                .withRegion(region)
                .build();
        this.mapper = new DynamoDBMapper(client);
        this.s3 = new MyS3Wrapper(region);
    }

    // create new user
    public boolean addUser(String username, String password){
        try {
            UserInfoSchema user = mapper.load(schemas.UserInfoSchema.class, username,
                    new DynamoDBMapperConfig(DynamoDBMapperConfig.ConsistentReads.CONSISTENT));

            // Is username already use?
            if(user == null){
                UserInfoSchema item = new UserInfoSchema(username, password);
                this.mapper.save(item);
                this.s3.createBucket(bucketPrefix + username);
                return true;
            }
            else{
                return false;
            }
        }
        catch(Exception e){
            return false;
        }
    }

    // login
    public boolean login(String username, String password){
        UserInfoSchema item = mapper.load(schemas.UserInfoSchema.class, username,
                new DynamoDBMapperConfig(DynamoDBMapperConfig.ConsistentReads.CONSISTENT));
        return item.getPassword().equals(password);
    }


    // upload
    public void put(String username, String fileName){
        String bucketName = bucketPrefix + username;
        // create template row
        FileInfoSchema item = new FileInfoSchema(username, username, fileName, bucketName);

        try{
            if(s3.addObjectToBucket(bucketName, fileName, fileName))
                this.mapper.save(item);
        } catch (Exception e){
            System.out.println("controller exception");
            Debugger.log(e.toString());
        }

    }


    public void view(String username){

        DynamoDBQueryExpression<FileInfoSchema> queryExpression = new DynamoDBQueryExpression<FileInfoSchema>()
                .withHashKeyValues(new FileInfoSchema(username));

        PaginatedQueryList<FileInfoSchema> viewableObjects = mapper.query(FileInfoSchema.class, queryExpression);
        for(FileInfoSchema obj : viewableObjects){
            try {
                // query only metadata -> save bandwidth
                s3.getObjectMeta(obj.getBucketName(), obj.getFileName());
            }
            catch(Exception E){
                E.printStackTrace();
            }
        }
    }


    public void share(String fileName, String from, String to){
        String bucketName = bucketPrefix + from;
        mapper.save(new FileInfoSchema(from, to, fileName, bucketName));
    }


    public void get(String fileName, String owner, String username){
        String owner_fileName = owner + "_" + fileName;

        // check wheter user can access the file
        FileInfoSchema item = mapper.load(FileInfoSchema.class, username, owner_fileName);

        if(item != null) {
            String bucketName = bucketPrefix + owner;
            s3.downloadObject(bucketName, fileName);
        }
    }

}
