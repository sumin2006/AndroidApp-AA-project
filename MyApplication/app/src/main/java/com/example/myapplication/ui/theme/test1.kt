// Step 3.1: Data Class
// data class는 데이터를 담기 위한 클래스로, equals(), hashCode(), toString() 등을 자동으로 생성해줍니다.
data class Book(val title: String, val author: String, val price: Int)

// Step 3.2: Enum Class
// enum class는 정해진 상수들의 집합을 나타낼 때 유용합니다.
enum class DayOfWeek(val koreanName: String) {
    MONDAY("월요일"),
    TUESDAY("화요일"),
    WEDNESDAY("수요일"),
    THURSDAY("목요일"),
    FRIDAY("금요일"),
    SATURDAY("토요일"),
    SUNDAY("일요일")
}

// DayOfWeek를 사용하는 함수
fun recommendActivity(day: DayOfWeek) {
    when(day) {
        DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> println("${day.koreanName}에는 휴식을 취하세요. 🏖️")
        else -> println("${day.koreanName}에는 열심히 공부하세요! 📚")
    }
}

// Step 3.3: Sealed Class
// sealed class는 상속 가능한 클래스들을 특정 그룹으로 묶어 관리할 때 사용됩니다.
// when과 함께 사용하면 else 없이도 모든 경우를 컴파일러가 체크해줘서 안전합니다.
sealed class Result {
    data class Success(val data: String) : Result()
    data class Error(val message: String) : Result()
}

// Result를 처리하는 함수
fun handleResult(result: Result) {
    when(result) {
        is Result.Success -> println("성공: ${result.data}")
        is Result.Error -> println("오류: ${result.message}")
    }
}

// Step 4: 실행을 위한 main 함수
fun main() {
    println("--- Data Class 예제 ---")
    val book1 = Book("코틀린 인 액션", "드미트리 제메로프", 30000)
    val book2 = Book("코틀린 인 액션", "드미트리 제메로프", 30000)
    println("책 정보: $book1") // toString() 자동 호출
    println("두 책은 동일한가? ${book1 == book2}") // equals() 자동 비교

    println("\n--- Enum Class 예제 ---")
    recommendActivity(DayOfWeek.FRIDAY)
    recommendActivity(DayOfWeek.SUNDAY)

    println("\n--- Sealed Class 예제 ---")
    val successResult = Result.Success("데이터를 성공적으로 가져왔습니다.")
    val errorResult = Result.Error("네트워크 연결이 끊겼습니다.")
    handleResult(successResult)
    handleResult(errorResult)
}