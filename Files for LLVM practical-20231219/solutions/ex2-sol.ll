@.strR = private unnamed_addr constant [3 x i8] c"%d\00", align 1

; Function Attrs: nounwind uwtable
define i32 @readInt() #0 {
  %x = alloca i32, align 4
  %1 = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.strR, i32 0, i32 0), i32* %x)
  %2 = load i32, i32* %x, align 4
  ret i32 %2
}

declare i32 @__isoc99_scanf(i8*, ...) #1

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

define i32 @exercise2(i32 %av, i32 %bv){
   entry:
      ; item 1
      %a = alloca i32
      %b = alloca i32
      ; item 2
      store i32 %av, i32* %a
      store i32 %bv, i32* %b
      ; item 3
      %0 = load i32, i32* %a
      %1 = add i32 5, %0
      store i32 %1, i32* %a
      ; item 4
      %2 = load i32, i32* %b
      %3 = sdiv i32 %2, 2
      store i32 %3, i32* %b
      ; item 5
      %4 = load i32, i32* %a
      %5 = load i32, i32* %b
      %6 = icmp sgt i32 %4, %5
      br i1 %6, label %outputa, label %outputb
   outputa:
      %7 = load i32, i32* %a
      ret i32 %7
   outputb:
      %8 = load i32, i32* %b
      ret i32 %8
}

define i32 @main() {
  entry:
  %a = call i32 @readInt()
  %b = call i32 @readInt()
  %0 = call i32 @exercise2(i32 %a, i32 %b)
  call void @println(i32 %0)
  ret i32 0
}
