;;;;;;;;;;; read me ;;;;;;;;;
;(1) %if 0 ... %endif same as block comment

;;;;;;;;;;; macro ;;;;;;;;;;;

%define loop_count 100

; temp hardcode 1000 long i.e. 8000
%define loop_length 16000

; input rdi:JENV*,rax:JENV*->GetPrimitiveArrayCritical*,rdx:jarray
; ouput rax
%macro GetPrimitiveArrayCritical 0
        mov rsi,rdx
        xor rdx,rdx
        call rax
%endmacro

; input rdi:JENV*,rcx:JENV*->ReleasePrimitiveArrayCritical*,rsi:jarray,rax:jarray*
; ouput null
%macro ReleasePrimitiveArrayCritical 0
        mov rdx,rax
	mov rax,rcx
        xor rcx,rcx
	call rax
%endmacro 

;;;;;;;;;;; external function declare ;;;;;;;;;;;

global Java_com_zyh_test_jni_BitwiseNativeOperate_print_1var_1addr:function
extern printf

;;;;;;;;;;; section data ;;;;;;;;;;;

section .data
	printfPattern db "value of (rdi,rsi,rdx,rcx) JNIEnv*[0x%x]jclass[0x%x]jlongArray1[0x%x]jlongArray2[0x%x]",10,0

;;;;;;;;;;; section text ;;;;;;;;;;;

section .text

; Java jni interface
;
; In Java class
; 	static native void print_var_addr(long[] al, long[] bl);
;
; In C proto
;	JNIEXPORT void JNICALL Java_com_zyh_test_jni_BitwiseNativeOperate_print_1var_1addr
;  (JNIEnv *, jclass, jlongArray, jlongArray);
;
; void * GetPrimitiveArrayCritical(JNIEnv *env, jarray array, jboolean *isCopy);
; 	index 222
; void ReleasePrimitiveArrayCritical(JNIEnv *env, jarray array, void *carray, jint mode);
; 	index 223
Java_com_zyh_test_jni_BitwiseNativeOperate_print_1var_1addr:
; call jni interface
	push rbp
	mov rbp,rsp
	sub rsp,56
	mov [rbp-8],rdi
	mov [rbp-16],rdx
	mov [rbp-24],rcx
	mov rsi,[rdi]
	mov rax,[rsi+1776]
	mov [rbp-32],rax
	mov r11,[rsi+1784]
	mov [rbp-40],r11
	GetPrimitiveArrayCritical
	mov [rbp-48],rax ;jarray1*
	mov rdi,[rbp-8]
	mov rdx,[rbp-24] ;jarray2
	mov rax,[rbp-32]
	GetPrimitiveArrayCritical
	mov r11,rax ;jarray2*
	mov rax,[rbp-48]
	xor rcx,rcx
	xor edi,edi
check_outer_loop_end:
	xor esi,esi
do_union:
	vmovdqu ymm0,[rax,rsi]
	vmovdqu ymm1,[r11,rsi]
	vorpd ymm0,ymm1,ymm0
	vmovdqu [rax,rsi],ymm0
	add esi,32
	inc rcx
	cmp esi,loop_length
	jne do_union
	inc edi
	cmp edi,loop_count
	jne check_outer_loop_end
release_ret:
	mov rax,r11
	mov [rbp-56],rcx
	;mov qword [r11],20090221
	;mov qword [r11+8],20090221
	mov rdi,[rbp-8]
	mov rsi,[rbp-24]
	mov rcx,[rbp-40]
	ReleasePrimitiveArrayCritical ;release jarray2*
	mov rdi,[rbp-8]
	mov rsi,[rbp-16]
	mov rcx,[rbp-40]
	mov rax,[rbp-48]
	ReleasePrimitiveArrayCritical ;release jarray1*
	mov rax,[rbp-56]
	leave
	ret

%if 0
; with register store temporary variables
Java_com_zyh_test_jni_BitwiseNativeOperate_print_1var_1addr:
; call jni interface
	push rbp
	mov rbp,rsp
	sub rsp,40
	push rbx
	push r12
	push r13
	push r14
	push r15
	mov rbx,rdi
	mov r12,rdx
	mov r13,rcx
	mov r14,r8

	mov [rbp-8],rdi
	mov [rbp-16],rdx
	mov [rbp-24],rcx
	mov rsi,[rdi]
	mov rax,[rsi+1776]
	mov [rbp-32],rax
	mov r11,[rsi+1784]
	mov [rbp-40],r11
	GetPrimitiveArrayCritical
	mov qword [rax],1978 ;change element
	mov qword [rax+8],20090221
	vmovdqu ymm0,yword [rax]
	vmovdqu yword [rax+32],ymm0
	mov rdi,[rbp-8]
	mov rsi,[rbp-16]
	ReleasePrimitiveArrayCritical
	pop r15
	pop r14
	pop r13
	pop r12
	pop rbx
	leave
	ret
%endif



; union two long[], result put in first long[]
union:
	
;;;;;;;;;;; reference or example ;;;;;;;;;;;	

; call print
%if 0	
	mov r11,rdi
	mov rdi,printfPattern
	xchg rsi,r11
	xchg rdx,r11
	xchg rcx,r11
	mov r8,r11
 	xor rax,rax
	jmp printf wrt ..plt
%endif

; dis-asm from c call GetPrimitiveArrayCritical and ReleasePrimitiveArrayCritical
%if 0
	push   rbp
	mov rbp,rsp
	sub rsp,0x30
	mov [rbp-0x18],rdi
	mov [rbp-0x20],rsi
	mov [rbp-0x28],rdx
	mov [rbp-0x30],rcx
	mov rax,[rbp-0x18]
	mov rax,[rax]
	mov rax,[rax+0x6f0]
	mov rsi,[rbp-0x28]
	mov rcx,[rbp-0x18]
	mov	edx,0x0
	mov rdi,rcx
	call rax
	mov [rbp-0x8],rax
	mov rax,[rbp-0x18]
	mov	rax,[rax]
	mov rax,[rax+0x6f8]
	mov	rdx,[rbp-0x8]
	mov rsi,[rbp-0x28]
	mov	rdi,[rbp-0x18]
	mov ecx,0x0
	call  rax
	leave 
	ret 
%endif
