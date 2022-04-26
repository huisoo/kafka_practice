package team.msa.member.presentation.member;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import team.msa.member.application.member.MemberApplicationService;
import team.msa.member.application.response.MemberBlahBlahResponse;
import team.msa.member.application.response.MemberInfoResponse;
import team.msa.member.application.response.MemberLoginResponse;
import team.msa.member.application.response.MemberRegistrationResponse;
import team.msa.member.domain.model.member.Member;
import team.msa.member.domain.model.member.MemberType;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
@RequiredArgsConstructor
public class MemberHandler {

    private final MemberApplicationService memberApplicationService;

    /**
     * 강사 등록
     * --> '사이트 운영자'는 강의 컨테츠를 업로드할 '강사' 회원을 생성할 수 있다.
     * @param request : 등록할 강사 정보
     * @return Mono<ServerResponse> : 등록된 강사 정보
     */
    public Mono<ServerResponse> teacherRegistration(ServerRequest request) {

        Mono<MemberRegistrationResponse> response = memberApplicationService.memberRegistration(request, MemberType.TEACHER)
            .subscribeOn(Schedulers.boundedElastic());

        return ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response, MemberRegistrationResponse.class);
    }

    /**
     * 학생 회원 가입
     * --> 강의를 수강하고자 하는 사람은 '학생'으로 회원 가입이 가능하다.
     * @param request : 가입할 학생 정보
     * @return Mono<ServerResponse> : 등록된 학생 회원 정보
     */
    public Mono<ServerResponse> studentRegistration(ServerRequest request) {

        Mono<MemberRegistrationResponse> response = memberApplicationService.memberRegistration(request, MemberType.STUDENT)
            .subscribeOn(Schedulers.boundedElastic());

        return ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response, MemberRegistrationResponse.class);
    }

    /**
     * 로그인
     * @param request : 로그인 정보
     * @return Mono<ServerResponse> : 권한 인증 정보
     */
    public Mono<ServerResponse> login(ServerRequest request) {

        Mono<MemberLoginResponse> response = memberApplicationService.login(request)
                .subscribeOn(Schedulers.boundedElastic());

        return ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response, MemberLoginResponse.class);
    }

    /**
     * 회원 정보 조회
     * @param request : 조회할 회원 정보
     * @return Mono<ServerResponse> : 조회된 회원 정보
     */
    public Mono<ServerResponse> findMemberInfo(ServerRequest request) {

        Mono<MemberInfoResponse> response = memberApplicationService.findMemberInfo(request)
                .subscribeOn(Schedulers.boundedElastic());

        return  ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(response, MemberInfoResponse.class);
                //ServerResponse.ok().//serverResponseFactory.successBodyValue(response, Member.class);
    }

}
