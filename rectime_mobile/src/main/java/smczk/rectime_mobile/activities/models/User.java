package smczk.rectime_mobile.activities.models;

/**
 * Created by shimon on 14/10/29.
 */
public class User {
    private String email;
    private String password;

    public String getEmail() { return email;}
    public String getPassword() { return password;}
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
