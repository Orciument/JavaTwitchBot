package talium.security.auth.persistence;

public record PanelUserDTO(
        String username,
        String userId,
        long accountCreationTime
) {
}
