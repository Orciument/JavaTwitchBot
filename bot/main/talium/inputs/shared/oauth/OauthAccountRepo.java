package talium.inputs.shared.oauth;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;

@Repository
public interface OauthAccountRepo extends CrudRepository<OauthAccount, String> {

    @NotNull ArrayList<OauthAccount> findAll();

    Optional<OauthAccount> getByAccNameAndService(String name, String service);
}
