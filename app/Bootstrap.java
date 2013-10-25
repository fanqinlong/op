import play.test.*;
import play.jobs.*;
import models.*;
import models.activity.Activity;
import models.users.SimpleUser;

@OnApplicationStart
public class Bootstrap extends Job {
    
    public void doJob() {
        if(SimpleUser.count() == 0) {
            Fixtures.loadModels("init-data-user.yml");
        }
        if(Activity.count()==0){
        	Fixtures.loadModels("init-data-activity.yml");
        }
    }
    
}