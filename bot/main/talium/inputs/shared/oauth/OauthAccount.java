package talium.inputs.shared.oauth;

import jakarta.persistence.*;

@Entity
@Table(name = "sys-auth_accounts")
public class OauthAccount {
    @Id
    public String accName;
    public String service;
    public String refreshToken;

    public OauthAccount() {
    }

    public OauthAccount(String accName, String service, String refreshToken) {
        this.accName = accName;
        this.service = service;
        this.refreshToken = refreshToken;
    }
}


