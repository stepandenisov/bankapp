package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Send notification successfully"

    request {
        method POST()
        url("/")
        headers {
            contentType(applicationJson())
        }
        body(
                message: "Hello, world!"
        )
    }

    response {
        status 200
        body("Уведомление отправлено")
        headers {
            contentType("text/plain;charset=UTF-8")
        }
    }
}
