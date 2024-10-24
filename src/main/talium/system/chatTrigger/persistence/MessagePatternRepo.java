package talium.system.chatTrigger.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessagePatternRepo extends CrudRepository<MessagePattern, String> {

}
