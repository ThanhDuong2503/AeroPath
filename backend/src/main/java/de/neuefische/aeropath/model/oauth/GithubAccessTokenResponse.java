package de.neuefische.aeropath.model.oauth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GithubAccessTokenResponse {
    private String access_token;
}
