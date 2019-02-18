package schemas;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="UserInfo")
public class UserInfoSchema {
    private String username;
    private String password;

    public UserInfoSchema(){

    }

    public UserInfoSchema(String username, String password){
        setUsername(username);
        setPassword(password);
    }


    @DynamoDBHashKey(attributeName="username")
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }


    @DynamoDBAttribute(attributeName="password")
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}