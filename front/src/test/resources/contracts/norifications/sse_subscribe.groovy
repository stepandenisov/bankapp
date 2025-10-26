package contracts.norifications

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Подписка на SSE уведомления"
    request {
        method GET()
        urlPath("/sse/notifications")
    }
    response {
        status 200
        body('')
    }
}
