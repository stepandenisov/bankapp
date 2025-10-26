package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Получает обменный курс"
    request {
        method GET()
        url "/rate"
    }
    response {
        status 200
        body(
                rate: [
                        [
                            currency: "USD",
                            value: 1.0
                        ],
                        [
                            currency: "RUB",
                            value: 90.0
                        ]
                ]
        )
        headers {
            contentType(applicationJson())
        }
    }
}
