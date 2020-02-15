package kr.co.didimu.ulotto.data.dto

data class LoginResponse(val userid: String ,
                         val userName : String ,
                         val handycap : Int ,
                         val email : String,
                         val profileImg : String) : Base(){
}