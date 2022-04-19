package hello.advanced.app.v3;

import hello.advanced.trace.TraceStatus;
import hello.advanced.trace.logtrace.LogTrace;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderControllerV3 {

    private final OrderServiceV3 orderService;
    private final LogTrace trace;

    @GetMapping("/v3/request")
    public String request(String itemId) {

        TraceStatus status = null;
        try {
            status = trace.begin("OrderController.request()");
            orderService.orderItem(itemId);
            trace.end(status);
            return "ok";
        } catch (Exception e) {
            trace.exception(status, e);
            throw e;
            // 예외를 꼭 다시 던져주어야 한다.
            // 그렇지 않으면 여기서 예외를 먹어버리고, 이후에 정상 흐름으로 동작한다.
            // 로그는 애플리케이션 흐름에 영향을 주면 안된다. 로그 때문에 예외가 사라지면 안된다.
        }
    }
}
