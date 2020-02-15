package kr.co.didimu.ulotto.data.dto

open class Base {


    var result: Int = -1
    var message : String = ""
    var sessionID : String = ""

    lateinit var inputdata: InputData

    data class InputData (val userid : String,
                          val passwd: String,
                          val os : String ,
                          val osversion : String,
                          val appversion : String )
}