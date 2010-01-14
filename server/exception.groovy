mail.send sender: "hurr@android-exceptions-handler.appspotmail.com",
to: "jecklandin@gmail.com",
subject: "Vkontakte: new exception",
textBody: "version: ${params.package_version}, package_name: ${params.package_name}, phone_model: ${params.phone_model}, android_version: ${params.android_version}, stacktrace: \r\n${params.stacktrace}"
println "ok"