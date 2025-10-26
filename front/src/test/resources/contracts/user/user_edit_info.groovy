package contracts.user

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Редактирование информации пользователя"
    request {
        method 'POST'
        url '/user/info'
        headers {
            contentType('multipart/form-data')
        }
        multipart([
                name: 'Updated User',
                birthday: '1991-01-01'
        ])
    }
    response {
        status 302
        headers {
            header('Location', '/')
        }
    }
}
