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
      %res   = alloca i32
      %digit = alloca i32
      store i32 0, i32* %res
      br label %read
   read:	                                  ; read a digit
      %0 = call i32 @getchar()
      %1 = sub i32 %0, 48
      store i32 %1, i32* %digit
      %2 = icmp ne i32 %0, 10               ; is the char entered '\n'?
      br i1 %2, label %check, label %exit
   check:                                   ; is the char entered a number?
      %3 = icmp sle i32 %1, 9
      %4 = icmp sge i32 %1, 0
      %5 = and i1 %3, %4
      br i1 %5, label %save, label %exit
   save:	                                  ; res<-res*10+digit
      %6 = load i32, i32* %res
      %7 = load i32, i32* %digit
      %8 = mul i32 %6, 10
      %9 = add i32 %8, %7
      store i32 %9, i32* %res
      br label %read
   exit:	                                  ; return res
      %10 = load i32, i32* %res
      ret i32 %10
}

define i32 @main() {
  entry:
  %x = call i32 @readInt()
  call void @println(i32 %x)
  ret i32 0
}
