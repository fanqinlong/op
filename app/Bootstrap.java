import play.test.*;
import play.jobs.*;
import models.*;
import models.users.SimpleUser;

@OnApplicationStart
public class Bootstrap extends Job {
    
    public void doJob() {
        if(SimpleUser.count() == 0) {
            Fixtures.loadYaml("test.yml");
        }
    }
    
}