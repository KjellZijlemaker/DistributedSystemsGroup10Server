package Beans;

import java.io.Serializable;

/**
 * This is one of the beans we will use to store all the data per object in.
 * For example: the battlefield could be such an object
 */
public class User implements Serializable{
    private static final long serialVersionUID = 1190476516911661470L;
    private String username;
    private String password;
    private String userID;

    public User(){
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
