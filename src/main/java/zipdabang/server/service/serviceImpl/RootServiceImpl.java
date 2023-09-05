package zipdabang.server.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zipdabang.server.auth.provider.TokenProvider;
import zipdabang.server.base.Code;
import zipdabang.server.base.exception.handler.RootException;
import zipdabang.server.domain.Category;
import zipdabang.server.domain.member.Member;
import zipdabang.server.repository.CategoryRepository;
import zipdabang.server.repository.memberRepositories.MemberRepository;
import zipdabang.server.service.RootService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RootServiceImpl implements RootService {

    private final CategoryRepository categoryRepository;

    private final MemberRepository memberRepository;

    private final TokenProvider tokenProvider;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Boolean autoLoginService(String authorizationHeader) {

        Boolean result = null;
        if(authorizationHeader == null)
            result = false;
        else{
            String token = authorizationHeader.substring(7);
            Long memberId = tokenProvider.validateAndReturnSubject(token);
            if (memberId.equals(0L))
                result = true;
            else{
                Member member = memberRepository.findById(memberId).orElseThrow(() -> new RootException(Code.MEMBER_NOT_FOUND));
                if(member.getAge() == null || member.getNickname() == null || member.getName() == null || member.getGender() == null)
                    result = false;
                else
                    result = true;
            }
        }

        return result;
    }
}
