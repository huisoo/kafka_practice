package team.msa.member.application.member;

import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import team.msa.member.application.response.MemberInfoResponse;
import team.msa.member.application.response.MemberLoginResponse;
import team.msa.member.application.response.MemberRegistrationResponse;
import team.msa.member.domain.model.member.*;
import team.msa.member.infrastructure.exception.status.BadRequestException;
import team.msa.member.infrastructure.exception.status.ExceptionMessage;
import team.msa.member.presentation.member.request.MemberLoginRequest;
import team.msa.member.domain.model.member.MemberSaveSpecification;
import team.msa.member.domain.model.member.MemberSearchSpecification;
import team.msa.member.domain.model.member.MemberType;
import team.msa.member.presentation.member.request.MemberRegistrationRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class MemberApplicationServiceImpl implements MemberApplicationService {

    private final MemberSaveSpecification memberSaveSpecification;
    private final MemberSearchSpecification memberSearchSpecification;

    private final MemberLoginSpecification memberLoginSpecification;

    private final static String BOOTSTRAP_SERVER = "my-kafka:9092";
    private final static String TOPIC_NAME = "basic_topic";


    /**
     * 회원 계정 생성
     * @param serverRequest : 전달된 Request
     * @param memberType : 회원 유형
     * @return Mono<MemberRegistrationResponse> : 저장된 회원 정보
     */
    @Override
    public Mono<MemberRegistrationResponse> memberRegistration(ServerRequest serverRequest, MemberType memberType) {

        return serverRequest.bodyToMono(MemberRegistrationRequest.class).flatMap(
            request -> {
                request.verify(); // Request 유효성 검사

                return memberSaveSpecification.memberExistCheckAndRegistration(request, memberType); // 회원 계정 생성
            }
        ).switchIfEmpty(Mono.error(new BadRequestException(ExceptionMessage.IsRequiredRequest.getMessage())));
    }

    @Override
    public Mono<MemberLoginResponse> login(ServerRequest serverRequest) {

        return serverRequest.bodyToMono(MemberLoginRequest.class).flatMap(
                request -> {
                    request.verify(); // Request 유효성 검사
                    return memberLoginSpecification.memberExistCheckAndLogin(request);
                }
        ).switchIfEmpty(Mono.error(new BadRequestException(ExceptionMessage.IsRequiredRequest.getMessage())));
    }

    @Override
    public Mono<MemberInfoResponse> findMemberInfo(ServerRequest request) {

        String memberIdStr = request.pathVariable("memberId");
        if (StringUtils.isBlank(memberIdStr)) throw new BadRequestException(ExceptionMessage.IsRequiredMemberId.getMessage());

        int memberId = Integer.parseInt(memberIdStr);

        /**
         * 프로듀서의 인스턴스에 사용할 '필수 옵션'을 설정한다.
         * [참고] 선언하지 않은 '선택 옵션'은 기본 옵션값으로 설정되어 동작한다.
         */
        Properties configs = new Properties();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);

        /**
         * 메시지 키, 값을 직렬화 하기 위해 StringSerializer를 사용한다.
         * StringSerializer는 String을 직렬화하는 카프카의 라이브러리이다.
         * (org.apache.kafka.common.serialization)
         */
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        /**
         * 프로듀서 인스턴스를 생성하며, 위에서 설정한 설정을 파라미터로 사용한다.
         */
        KafkaProducer<String, String> producer = new KafkaProducer<>(configs);

        /**
         * 전달할 메시지 값을 생성한다.
         * (여기서는 애플리케이션 실행 시점의 날짜와 시간을 조합하여서 메시지 값으로 생성한다.)
         */
        Date todayDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String messageValue = "getMemberInfo [" + dateFormat.format(todayDate) + "]" + memberIdStr;

        /**
         * 레코드를 생성하고 전달한다.
         * 이때, 레코드를 전달할 토픽과 레코드의 메시지 값을 지정한다.
         */
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, messageValue);
        producer.send(record);

        return memberSearchSpecification.getMemberInfo(memberId);
    }
}
