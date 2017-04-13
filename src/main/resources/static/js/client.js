/**
 * Created by stephan on 20.03.16.
 */

$(function () {
    // VARIABLES =============================================================
    var TOKEN_KEY = "jwtToken"

    var $anonymousTitle = $("#anonymousTitle");
    var $authenticatedTitle = $("#authenticatedTitle").hide();
    
    var $anonymousBody = $("#anonymousBody");
    var $authenticatedBody = $("#authenticatedBody").hide();

    var $requestResponse = $("#response");
    
    // FUNCTIONS =============================================================
    function getJwtToken() {
        return localStorage.getItem(TOKEN_KEY);
    }

    function setJwtToken(token) {
        localStorage.setItem(TOKEN_KEY, token);
    }

    function removeJwtToken() {
        localStorage.removeItem(TOKEN_KEY);
    }

    function doLogin(loginData) {
        $.ajax({
            url: "/auth",
            type: "POST",
            data: JSON.stringify(loginData),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (data, textStatus, jqXHR) {
                setJwtToken(data.token);
                showLoggedUserInfo();
            },
            error: function (jqXHR, textStatus, errorThrown) {
                if (jqXHR.status === 401) {
                    $('#loginErrorModal')
                        .modal("show")
                        .find(".modal-body")
                        .empty()
                        .html("<p>Spring exception:<br>" + jqXHR.responseJSON.exception + "</p>");
                } else {
                    throw new Error("an unexpected error occured: " + errorThrown);
                }
            }
        });
    }

    function createAuthorizationTokenHeader() {
        var token = getJwtToken();
        if (token) {
            return {"Authorization": token};
        } else {
            return {};
        }
    }

    function showLoggedUserInfo() {
        // Show raw JWT
        $anonymousTitle.hide();
        $authenticatedTitle.text("Token: " + getJwtToken()).show();

        // Show JWT content
        $anonymousBody.hide();

        var tokenDataHolder = $authenticatedBody.find("#tokenData");
        tokenDataHolder.text(JSON.stringify(jwt_decode(getJwtToken()), undefined, 2));
        $authenticatedBody.show();
    }

    function logoutUser() {
        removeJwtToken();

        $authenticatedTitle.hide()
        $anonymousTitle.show();

        $authenticatedBody.hide()
        $anonymousBody.show();
    }

    function showResponse(statusCode, message) {
        $requestResponse
            .empty()
            .text("status code: " + statusCode + "\n-------------------------\n" + message);
    }

    // REGISTER EVENT LISTENERS =============================================================
    $("#loginForm").submit(function (event) {
        event.preventDefault();

        var $form = $(this);
        var formData = {
            username: $form.find('input[name="username"]').val(),
            password: $form.find('input[name="password"]').val()
        };

        doLogin(formData);
    });

    $("#logoutButton").click(logoutUser);

    $("#authServiceBtn").click(function () {
        $.ajax({
            url: "/authenticated",
            type: "GET",
            contentType: "application/json; charset=utf-8",
            headers: createAuthorizationTokenHeader(),
            success: function (data, textStatus, jqXHR) {
                showResponse(jqXHR.status, data);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                showResponse(jqXHR.status, errorThrown);
            }
        });
    });

    $("#adminServiceBtn").click(function () {
        $.ajax({
            url: "/admin",
            type: "GET",
            contentType: "application/json; charset=utf-8",
            headers: createAuthorizationTokenHeader(),
            success: function (data, textStatus, jqXHR) {
                showResponse(jqXHR.status, data);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                showResponse(jqXHR.status, errorThrown);
            }
        });
    });

    // INITIAL CALLS =============================================================
    if (getJwtToken()) {
        // Remove previously stored JWT when page loads
        removeJwtToken();
    }
});
