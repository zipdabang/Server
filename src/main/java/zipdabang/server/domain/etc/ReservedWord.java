package zipdabang.server.domain.etc;

import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@NoArgsConstructor
@DiscriminatorValue("RESERVED")
public class ReservedWord extends BannedWord{
    public ReservedWord(String word) {
        super(word);
    }

}
