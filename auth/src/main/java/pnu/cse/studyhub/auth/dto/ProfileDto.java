package pnu.cse.studyhub.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class ProfileDto {
    private String id;

    private String name;
    private String email;
    private String profileImage;
    private String description;
    private String studyTime;
    private String url;
}
