extern crate jni;
extern crate libc;
extern crate weld;
extern crate regex;

use libc::c_void;

use jni::JNIEnv;
use jni::objects::{JByteBuffer, JObject, JString};
use jni::sys::{jstring, jlong, jint};
use jni::strings::*;

use weld::ffi::*;

// TODO Temporary measure. We should spin this of in a different library.
pub mod utf8lib;

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_00024_weld_1context_1new(_: JNIEnv, _: JObject, confPtr: jlong) -> jlong {
    let conf = confPtr as WeldConfRef;
    let value = weld_context_new(conf);
    value as jlong
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_00024_weld_1context_1memory_1usage(_: JNIEnv, _: JObject, contextPtr: jlong) -> jlong {
    let context = contextPtr as WeldContextRef;
    let value = weld_context_memory_usage(context);
    value as jlong
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_00024_weld_1context_1free(_: JNIEnv, _: JObject, contextPtr: jlong) {
    let context = contextPtr as WeldContextRef;
    weld_context_free(context)
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_00024_weld_1value_1new(_: JNIEnv, _: JObject, dataPtr: jlong) -> jlong {
    let data = dataPtr as *const c_void;
    let value = weld_value_new(data);
    value as jlong
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_00024_weld_1value_1pointer(_: JNIEnv, _: JObject, valuePtr: jlong) -> jlong {
    let value = valuePtr as WeldValueRef;
    let data = weld_value_data(value);
    data as jlong
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_00024_weld_1value_1context(_: JNIEnv, _: JObject, valuePtr: jlong) -> jlong {
    let value = valuePtr as WeldValueRef;
    let data = weld_value_context(value);
    data as jlong
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_00024_weld_1get_1buffer_1pointer(env: JNIEnv, _: JObject, buffer: JByteBuffer) -> jlong {
    let data = env.get_direct_buffer_address(buffer);
    data.unwrap().as_ptr() as jlong
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_00024_weld_1value_1run(_: JNIEnv, _: JObject, valuePtr: jlong) -> jlong {
    let value = valuePtr as WeldValueRef;
    weld_value_run(value)
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_00024_weld_1value_1free(_: JNIEnv, _: JObject, valuePtr: jlong) {
    let value = valuePtr as WeldValueRef;
    weld_value_free(value)
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_00024_weld_1module_1compile(env: JNIEnv, _: JObject, jcode: JString, confPtr: jlong, errorPtr: jlong) -> jlong {
    let conf = confPtr as WeldConfRef;
    let error = errorPtr as WeldErrorRef;
    let code = env.get_string(jcode).unwrap();
    let module = weld_module_compile(code.get_raw(), conf, error);
    module as jlong
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_00024_weld_1module_1run(_: JNIEnv, _: JObject, modulePtr: jlong, contextPtr: jlong, inputPtr: jlong, errorPtr: jlong) -> jlong {
    let module = modulePtr as WeldModuleRef;
    let context = contextPtr as WeldContextRef;
    let input = inputPtr as WeldValueRef;
    let error = errorPtr as WeldErrorRef;
    let result = weld_module_run(module, context, input, error);
    result as jlong
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_00024_weld_1module_1free(_: JNIEnv, _: JObject, modulePtr: jlong) {
    let module = modulePtr as WeldModuleRef;
    weld_module_free(module)
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_00024_weld_1error_1new(_: JNIEnv, _: JObject) -> jlong {
    let error = weld_error_new();
    error as jlong
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_00024_weld_1error_1free(_: JNIEnv, _: JObject, errorPtr: jlong) {
    let error = errorPtr as WeldErrorRef;
    weld_error_free(error)
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_00024_weld_1error_1code(_: JNIEnv, _: JObject, errorPtr: jlong) -> jint {
    let error = errorPtr as WeldErrorRef;
    weld_error_code(error) as jint
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_00024_weld_1error_1message(env: JNIEnv, _: JObject, errorPtr: jlong) -> jstring {
    let error = errorPtr as WeldErrorRef;
    let message = JNIStr::from_ptr(weld_error_message(error)).to_owned();
    env.new_string(message).unwrap().into_inner()
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_00024_weld_1conf_1new(_: JNIEnv, _: JObject) -> jlong {
    let conf = weld_conf_new();
    conf as jlong
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_00024_weld_1conf_1free(_: JNIEnv, _: JObject, confPtr: jlong) {
    let conf = confPtr as WeldConfRef;
    weld_conf_free(conf)
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_00024_weld_1conf_1get(env: JNIEnv, _: JObject, confPtr: jlong, jkey: JString) -> jstring {
    let conf = confPtr as WeldConfRef;
    let key = env.get_string(jkey).unwrap();
    let valuePtr = weld_conf_get(conf, key.get_raw());
    if valuePtr.is_null() {
        std::ptr::null_mut()
    } else {
        let value = JNIStr::from_ptr(valuePtr).to_owned();
        env.new_string(value).unwrap().into_inner()
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_00024_weld_1conf_1set(env: JNIEnv, _: JObject, confPtr: jlong, jkey: JString, jvalue: JString) {
    let conf = confPtr as WeldConfRef;
    let key = env.get_string(jkey).unwrap();
    let value = env.get_string(jvalue).unwrap();
    weld_conf_set(conf, key.get_raw(), value.get_raw())
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_00024_weld_1load_1library(env: JNIEnv, _: JObject, jfilename: JString, errorPtr: jlong) {
    let filename = env.get_string(jfilename).unwrap();
    let error = errorPtr as WeldErrorRef;
    weld_load_library(filename.get_raw(), error)
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_00024_weld_1set_1log_1level(env: JNIEnv, _: JObject, level_str: JString) {
    let level = String::from(env.get_string(level_str).unwrap()).to_lowercase();
    if level == "error" {
        weld_set_log_level(WeldLogLevel::Error);
    } else if level == "warn" {
        weld_set_log_level(WeldLogLevel::Warn);
    } else if level == "info" {
        weld_set_log_level(WeldLogLevel::Info);
    } else if level == "debug" {
        weld_set_log_level(WeldLogLevel::Debug);
    } else if level == "trace" {
        weld_set_log_level(WeldLogLevel::Trace);
    } else if level == "off" {
        weld_set_log_level(WeldLogLevel::Off);
    }
}
