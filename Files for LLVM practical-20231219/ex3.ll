@.strP = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

; Function Attrs: nounwind uwtable
define void @println(i32 %x) #0 {
  %1 = alloca i32, align 4
  store i32 %x, i32* %1, align 4
  %2 = load i32, i32* %1, align 4
  %3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.strP, i32 0, i32 0), i32 %2)
  ret void
}

declare i32 @printf(i8*, ...) #1

declare i32 @getchar()

define i32 @readInt() {
   entry:                                 ; create variables

   read                                   ; read a digit

                                          ; Do your computations

   exit:	                               ; return res

}

define i32 @main() {
  entry:
  %x = call i32 @readInt()
  call void @println(i32 %x)
  ret i32 0
}
