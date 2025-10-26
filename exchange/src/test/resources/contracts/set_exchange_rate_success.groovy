package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Устанавливает курс"
    request {
        method POST()
        url "/rate"
        body(
                exchangeRate: [
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
    response {
        status 200
    }
}
