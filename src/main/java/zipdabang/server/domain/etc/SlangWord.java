package zipdabang.server.domain.etc;

import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@NoArgsConstructor
@DiscriminatorValue("SLANG")
public class SlangWord extends BannedWord{
    public SlangWord(String word) {
        super(word);
    }
}
