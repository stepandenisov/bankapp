package contracts.norifications

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Отправка уведомления"
    request {
        method 'POST'
        url '/notification'
        headers {
            contentType('application/json')
        }
        body(
                userId: 1,
                message: "Hello"
        )
    }
    response {
        status 200
    }
}
