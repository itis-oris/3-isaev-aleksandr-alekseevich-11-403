document.addEventListener('DOMContentLoaded', function () {

    const loginInput = document.getElementById('login');
    const emailInput = document.getElementById('email');
    const form = document.querySelector('form');
    const passwordInput = document.getElementById('password');

    function checkField(inputElement, url, errorMessage) {
        const value = inputElement.value.trim();

        const errorDiv = document.querySelector(
            '[data-field="' + inputElement.id + '"]'
        );

        if (!value) {
            errorDiv.textContent = ''
            return
        }
        fetch(url + '?value=' + encodeURIComponent(value), {
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
            .then( function (response) {
                return response.json();
            })
            .then(function (data) {
                if (data.exists) {
                    errorDiv.textContent = errorMessage;
                    errorDiv.style.color = 'red';
                } else {
                    errorDiv.textContent = '';
                }
            })
            .catch(function (error) {
                console.error('Ошибки проверки: ', error);
            })
    }

    loginInput.addEventListener('blur', function () {
        checkField(this, '/api/check/login', 'Этот логин уже занят')
    })

            emailInput.addEventListener('blur', function () {
        checkField(this, '/api/check/email', 'Этот email уже занят')
    })

    document.querySelector('form').addEventListener('submit', function (event) {
        const errorDivs = document.querySelectorAll('.ajax-error');
        let hasErrors = false;

        errorDivs.forEach(function (div) {
            if (div.textContent !== '') {
                hasErrors = true;
            }
        });

        if (hasErrors) {
            event.preventDefault();
        } else {
            errorDivs.forEach(function (div) {
                div.textContent = '';
            });
        }
    });

    passwordInput.addEventListener('blur', function () {
        const errorDiv = document.querySelector('[data-field="password"]');
        const value = this.value;

        if (!value) {
            errorDiv.textContent = '';
        } else if (value.length < 8) {
            errorDiv.textContent = 'Пароль должен содержать минимум 8 символов'
        } else if (!/(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])/.test(value)) {
            errorDiv.textContent = 'Нужна цифра, строчная и заглавная буква';
            errorDiv.style.color = 'red';
        } else {
            errorDiv.textContent = '';
        }
    })
})