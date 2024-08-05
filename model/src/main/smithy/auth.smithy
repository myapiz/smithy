$version: "2"

namespace com.myapiz.smithy.auth

use alloy#simpleRestJson

enum Permission {
    READ = "read"
    WRITE = "write"
    EXECUTE = "execute"
}

list PermissionList {
    member: Permission
}

@trait(selector: "operation")
structure authorization {
    @required
    allow: PermissionList
}
