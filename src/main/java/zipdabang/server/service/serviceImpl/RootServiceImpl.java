package zipdabang.server.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zipdabang.server.domain.Category;
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

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
