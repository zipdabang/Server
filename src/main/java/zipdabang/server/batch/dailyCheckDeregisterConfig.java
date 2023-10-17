package zipdabang.server.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import zipdabang.server.domain.member.Member;
import zipdabang.server.service.MemberService;

import java.util.List;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class dailyCheckDeregisterConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final MemberService memberService;


    @Bean
    public Job deleteDeregisterMember(Step DeleteDeregisterMemberFirstStep) {
        return jobBuilderFactory.get("delete deregister member")
                .incrementer(new RunIdIncrementer())
                .start(DeleteDeregisterMemberFirstStep)
                .build();
    }

    @Bean
    public Step DeleteDeregisterMemberFirstStep(ItemReader<Member> memberItemReader, ItemProcessor<Member, Member> memberItemProcessor, ItemWriter<Member> memberItemWriter) {
        return stepBuilderFactory.get("firstStep")
                .<Member, Member>chunk(100)
                .reader(memberItemReader)
                .processor(memberItemProcessor)
                .writer(memberItemWriter)
                .build();
    }

    @Bean
    public ItemWriter<Member> memberItemWriter(){
        return items -> {};
    }

    @Bean
    @Transactional
    public ItemReader<Member> memberItemReader(){
        log.info("Batch Reader ===>");
        List<Member> targetMembers = memberService.getInactiveMembers();
        StringBuilder sb = new StringBuilder();
        for (Member m : targetMembers) {
            sb.append(" ").append(m.getNickname());
        }
        log.info("삭제 대상 Member 닉네임 알람 ===> {}", sb);
        return new ListItemReader<>(targetMembers);
    }

    @Bean
    public ItemProcessor<Member, Member> memberItemProcessor() {
        return member -> {
            memberService.deleteMemberInfo(member);
            return null;
        };
    }
}
