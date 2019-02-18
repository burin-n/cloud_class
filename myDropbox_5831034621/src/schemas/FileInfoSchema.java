package schemas;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

@DynamoDBTable(tableName="FileInfo")
public class FileInfoSchema {
    private String share;
    private String bucketName;
    private String owner_fileName;
    private String fileName;
    private String owner;

    public FileInfoSchema() {

    }

    public FileInfoSchema(String share) {
        setShare(share);
    }

    public FileInfoSchema(String owner, String share, String fileName, String bucketName) {
        setShare(share);
        setBucketName(bucketName);
        setOwner(owner);
        setFileName(fileName);
    }

    @DynamoDBHashKey(attributeName = "share")
    public String getShare() {
        return share;
    }

    public void setShare(String share) {
        this.share = share;
    }


    @DynamoDBRangeKey(attributeName = "owner_fileName")
    public String getOwner_fileName(){ return this.owner_fileName; }

    public void setOwner_fileName(String owner_fileName) {
        String owner = owner_fileName.split("_")[0];
        String fileName = owner_fileName.split("_")[1];
        setOwner(owner);
        setFileName(fileName);
    }

    private void setOwner_fileName(){
        this.owner_fileName = owner + "_" + fileName;
    }


    @DynamoDBAttribute(attributeName = "bucketName")
    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }


    @DynamoDBIgnore
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        this.setOwner_fileName();
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
        this.setOwner_fileName();
    }

    public String toString() {
        return share + " " + owner_fileName + " " + bucketName;
    }

}