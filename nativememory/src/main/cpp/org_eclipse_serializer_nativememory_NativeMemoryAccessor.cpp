/*-
 * #%L
 * Eclipse Serializer native memory
 * %%
 * Copyright (C) 2023 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

#include "org_eclipse_serializer_nativememory_NativeMemoryAccessor.h"
#include <stdlib.h>
#include <string.h>
						
JNIEXPORT jlong JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_getDirectByteBufferAddress(JNIEnv* env, jobject object, jobject directByteBuffer)
{
	return (jlong) env->GetDirectBufferAddress(directByteBuffer);
}

JNIEXPORT jlong JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_allocateMemory(JNIEnv* env, jobject object, jlong size)
{
	return (jlong)malloc(size);
}

JNIEXPORT jlong JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_reallocateMemory(JNIEnv* env, jobject object, jlong address, jlong size)
{
	return (jlong)realloc((void*)address, (size_t)size);
}

JNIEXPORT void JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_freeMemory(JNIEnv* env, jobject object, jlong address)
{
	free((void*)address);
}

JNIEXPORT void JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_fillMemory(JNIEnv* env, jobject object, jlong address, jlong size, jbyte value)
{
	memset((void*)address, (int)value, (size_t)size);
}

JNIEXPORT jbyte JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_get_1byte__J(JNIEnv* env, jobject object, jlong address)
{
	return *(jbyte*)address;
}

JNIEXPORT jboolean JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_get_1boolean__J(JNIEnv* env, jobject object , jlong address)
{
	return *(jboolean*)address;
}

JNIEXPORT jshort JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_get_1short__J(JNIEnv* env, jobject object, jlong address)
{
	return *(jshort*)address;
}

JNIEXPORT jchar JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_get_1char__J(JNIEnv* env, jobject object, jlong address)
{
	return *(jchar*)address;
}

JNIEXPORT jint JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_get_1int__J(JNIEnv* env, jobject object, jlong address)
{
	return *(jint*)address;
}

JNIEXPORT jfloat JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_get_1float__J(JNIEnv* env, jobject object, jlong address)
{
	return *(jfloat*)address;
}

JNIEXPORT jlong JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_get_1long__J(JNIEnv* env, jobject object, jlong address)
{	
	return *(jlong*)address;
}

JNIEXPORT jdouble JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_get_1double__J(JNIEnv* env, jobject object, jlong address)
{
	return *(jdouble*)address;
}

JNIEXPORT jbyte JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_get_1byte__Ljava_lang_Object_2J(JNIEnv* env, jobject object, jobject target, jlong fieldID)
{
	return env->GetByteField(target, (jfieldID)fieldID);
}

JNIEXPORT jboolean JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_get_1boolean__Ljava_lang_Object_2J(JNIEnv* env, jobject object, jobject target, jlong fieldID)
{
	return env->GetBooleanField(target, (jfieldID)fieldID);
}

JNIEXPORT jshort JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_get_1short__Ljava_lang_Object_2J(JNIEnv* env, jobject object, jobject target, jlong fieldID)
{
	return env->GetShortField(target, (jfieldID)fieldID);
}

JNIEXPORT jchar JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_get_1char__Ljava_lang_Object_2J(JNIEnv* env, jobject object, jobject target, jlong fieldID)
{
	return env->GetCharField(target, (jfieldID)fieldID);
}

JNIEXPORT jint JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_get_1int__Ljava_lang_Object_2J(JNIEnv* env, jobject object, jobject target, jlong fieldID)
{
	return env->GetIntField(target, (jfieldID)fieldID);
}

JNIEXPORT jfloat JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_get_1float__Ljava_lang_Object_2J(JNIEnv* env, jobject object, jobject target, jlong fieldID)
{
	return env->GetFloatField(target, (jfieldID)fieldID);
}

JNIEXPORT jlong JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_get_1long__Ljava_lang_Object_2J(JNIEnv* env, jobject object, jobject target, jlong fieldID)
{
	return env->GetLongField(target, (jfieldID)fieldID);
}

JNIEXPORT jdouble JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_get_1double__Ljava_lang_Object_2J(JNIEnv* env, jobject object, jobject target, jlong fieldID)
{
	return env->GetDoubleField(target, (jfieldID)fieldID);
}

JNIEXPORT jobject JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_getObject(JNIEnv* env , jobject object, jobject target, jlong fieldID)
{
	return env->GetObjectField(target, (jfieldID)fieldID);
}

JNIEXPORT void JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_set_1byte__JB(JNIEnv* env, jobject object, jlong address, jbyte value)
{
	*(jbyte*)address = value;
}

JNIEXPORT void JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_set_1boolean__JZ(JNIEnv* env, jobject object, jlong address, jboolean value)
{
	*(jboolean*)address = value;
}

JNIEXPORT void JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_set_1short__JS(JNIEnv* env, jobject object, jlong address, jshort value)
{
	*(jshort*)address = value;
}

JNIEXPORT void JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_set_1char__JC(JNIEnv* env, jobject object, jlong address, jchar value)
{
	*(jchar*)address = value;
}

JNIEXPORT void JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_set_1int__JI(JNIEnv* env, jobject object, jlong address, jint value)
{
	*(jint*)address = value;
}

JNIEXPORT void JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_set_1float__JF(JNIEnv* env, jobject object, jlong address, jfloat value)
{
	*(jfloat*)address = value;
}

JNIEXPORT void JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_set_1long__JJ(JNIEnv* env, jobject object, jlong address, jlong value)
{
	*(jlong*)address = value;
}

JNIEXPORT void JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_set_1double__JD(JNIEnv* env, jobject object, jlong address, jdouble value)
{
	*(jdouble*)address = value;
}

JNIEXPORT void JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_set_1byte__Ljava_lang_Object_2JB(JNIEnv* env, jobject object, jobject target, jlong fieldID, jbyte value)
{
	env->SetByteField(target, (jfieldID)fieldID, value);
}

JNIEXPORT void JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_set_1boolean__Ljava_lang_Object_2JZ(JNIEnv* env, jobject object, jobject target, jlong fieldID, jboolean value)
{
	env->SetBooleanField(target, (jfieldID)fieldID, value);
}

JNIEXPORT void JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_set_1short__Ljava_lang_Object_2JS(JNIEnv* env, jobject object, jobject target, jlong fieldID, jshort value)
{
	env->SetShortField(target, (jfieldID)fieldID, value);
}

JNIEXPORT void JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_set_1char__Ljava_lang_Object_2JC(JNIEnv* env, jobject object, jobject target, jlong fieldID, jchar value)
{
	env->SetCharField(target, (jfieldID)fieldID, value);
}

JNIEXPORT void JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_set_1int__Ljava_lang_Object_2JI(JNIEnv* env, jobject object, jobject target, jlong fieldID, jint value)
{
	env->SetIntField(target, (jfieldID)fieldID, value);
}

JNIEXPORT void JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_set_1float__Ljava_lang_Object_2JF(JNIEnv* env, jobject object, jobject target, jlong fieldID, jfloat value)
{
	env->SetFloatField(target, (jfieldID)fieldID, value);
}

JNIEXPORT void JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_set_1long__Ljava_lang_Object_2JJ(JNIEnv* env, jobject object, jobject target, jlong fieldID, jlong value)
{
	env->SetLongField(target, (jfieldID)fieldID, value);
}

JNIEXPORT void JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_set_1double__Ljava_lang_Object_2JD(JNIEnv* env, jobject object, jobject target, jlong fieldID, jdouble value)
{
	env->SetDoubleField(target, (jfieldID)fieldID, value);
}

JNIEXPORT void JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_setObject(JNIEnv* env, jobject object, jobject target, jlong fieldID, jobject value)
{
	env->SetObjectField(target, (jfieldID)fieldID, value);
}

JNIEXPORT jlong JNICALL Java_org_eclipse_serializer_nativememory_NativeMemoryAccessor_objectFieldOffset(JNIEnv* env, jobject object, jobject field)
{
	return (jlong)env->FromReflectedField(field);
}
