package org.eclipse.serializer.util;

/*-
 * #%L
 * Eclipse Serializer Base
 * %%
 * Copyright (C) 2023 - 2024 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

/**
 * Helper class to identify the current VM
 */
public interface VMInfo
{
	/**
	 * Returns true if the current jvm is the default jvm.
	 * that supports all java features.
	 * 
	 * @return true if default jvm
	 */
	public boolean isStandardJava();
	
	/**
	 * Returns true if the current jvm is a default android jvm.
	 * 
	 * @return true if default android
	 */
	public boolean isStandardAndroid();
	
	/**
	 * Returns true if the current jvm a GraalVm native image.
	 * 
	 * @return if GraalVm native image.
	 */
	public boolean isGraalVmNativeImage();
	
	/**
	 * Returns true if the current jvm any kind if android.
	 * 
	 * @return true if any android.
	 */
	public boolean isAnyAndroid();
	
	public static VMInfo New()
	{
		return new VMInfo.Default();
	}
	
	public static class Default implements VMInfo
	{
		private static final String ANY_ANDROID_VENDOR     = "android";
		private static final String DEFAULT_ANDROID_VENDOR = "The Android Project";
				
		private static final String JAVA_VENDOR                       = System.getProperty("java.vendor", "").toLowerCase();
		private static final String JAVA_VM_VENDOR                    = System.getProperty("java.vm.vendor", "").toLowerCase();
		private static final String ORG_GRAALVM_NATIVEIMAGE_IMAGECODE = System.getProperty("org.graalvm.nativeimage.imagecode","").toLowerCase();
		
		private static final boolean ANY_ANDROID         = checkAnyAndroid();
		private static final boolean GRAALVM_NATIVEIMAGE = checkGraalVMNativeImage();
		private static final boolean DEFAULT_ANDROID     = checkStandardAndroid();
		private static final boolean DEFAULT_JAVA        = checkStandardJavaVM();
						
				
		@Override
		public final boolean isStandardJava()
		{
			return DEFAULT_JAVA;
		}

		@Override
		public final boolean isAnyAndroid()
		{
			return ANY_ANDROID;
		}

		@Override
		public final boolean isGraalVmNativeImage()
		{
			return GRAALVM_NATIVEIMAGE;
		}

		@Override
		public final boolean isStandardAndroid()
		{
			return DEFAULT_ANDROID;
		}

		private static boolean checkStandardJavaVM()
		{
			return
				!checkStandardAndroid() &&
				!checkAnyAndroid() &&
				!checkGraalVMNativeImage()
			;
		}
	
		private static boolean checkStandardAndroid()
		{
			return
				JAVA_VENDOR.equalsIgnoreCase(DEFAULT_ANDROID_VENDOR) ||
				JAVA_VM_VENDOR.equalsIgnoreCase(DEFAULT_ANDROID_VENDOR)
			;
		}
		
		private static boolean checkAnyAndroid()
		{
			return
				JAVA_VENDOR.contains(ANY_ANDROID_VENDOR) ||
				JAVA_VM_VENDOR.contains(ANY_ANDROID_VENDOR)
			;
		}
		
		private static boolean checkGraalVMNativeImage()
		{
			return !ORG_GRAALVM_NATIVEIMAGE_IMAGECODE.isBlank();
		}
		
	}
		
}
