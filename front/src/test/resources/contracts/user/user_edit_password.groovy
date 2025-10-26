package contracts.user

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Редактирование пароля"
    request {
        method 'POST'
        url '/user/password'
        headers {
            contentType('multipart/form-data')
        }
        multipart([
                password: 'newpassword',
                confirmPassword: 'newpassword'
        ])
    }
    response {
        status 302
        headers {
            header('Location', '/')
        }
    }
}
