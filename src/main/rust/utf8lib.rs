extern crate regex;

use std::slice;
use std::str;

use regex::Regex;

#[derive(Clone,Debug)]
#[repr(C)]
pub struct WeldString {
    data: *const u8,
    len: i64
}

impl WeldString {
    pub unsafe fn as_slice(&self) -> &[u8] {
        let len = self.len as usize;
        slice::from_raw_parts(self.data, len)
    }

    pub unsafe fn as_str(&self) -> &str {
        str::from_utf8(self.as_slice()).unwrap()
    }

    pub unsafe fn as_str_unchecked(&self) -> &str {
        str::from_utf8_unchecked(self.as_slice())
    }
}

#[no_mangle]
pub unsafe extern "C" fn utf8_starts_with(
        text: *const WeldString, query: *const WeldString, result: *mut bool) {
    let text = &*text;
    let query = &*query;
    *result = text.as_slice().starts_with(query.as_slice())
}

#[no_mangle]
pub unsafe extern "C" fn utf8_ends_with(
        text: *const WeldString, query: *const WeldString, result: *mut bool) {
    let text = &*text;
    let query = &*query;
    *result = text.as_slice().ends_with(query.as_slice())
}

#[no_mangle]
pub unsafe extern "C" fn utf8_contains(
        text: *const WeldString, query: *const WeldString, result: *mut bool) {
    let text = &*text;
    let query = &*query;
    *result = text.as_str().contains(query.as_str())
}

/// Compile a regex and return a handle to it as an i64. In reality, the handle
/// will encode a pointer.
#[no_mangle]
pub unsafe extern "C" fn utf8_regex_compile(
        expression: *const WeldString, result: *mut i64) {
    let expression = &*expression;
    let re = Regex::new(expression.as_str()).unwrap();
    *result = Box::into_raw(Box::new(re)) as i64;
}

/// Apply a compiled regex to a string and return whether it matches.
#[no_mangle]
pub unsafe extern "C" fn utf8_regex_matches(
        text: *const WeldString, regex_handle: *mut i64, result: *mut bool) {
    let regex_ptr = (*regex_handle) as *mut Regex;
    let text = &*text;
    *result = (&*regex_ptr).is_match(text.as_str());
}

/// Is anyone really going to call this?
#[no_mangle]
pub unsafe extern "C" fn utf8_regex_destroy(regex_handle: *mut i64) {
    if *regex_handle != 0 {
        let regex_ptr = (*regex_handle) as *mut Regex;
        Box::from_raw(regex_ptr);
    }
}
