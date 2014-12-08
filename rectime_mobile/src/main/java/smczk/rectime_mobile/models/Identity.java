package smczk.rectime_mobile.models;

/**
 * Created by shimon on 14/12/07.
 */
public class Identity {
    private User user = new User();

    public User getUser() { return user;}
    public void setUser(User user) {
        this.user = user;
    }
}
