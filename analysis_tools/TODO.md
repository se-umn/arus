# TODO

* Check whether we need to deal with @RunWith(MockitoSettings.Strictness.STRICT)
* FIX logging in case of super in constructor
* Maybe use the DefaultInternalRunner to log when a constructor is invoked (BlockJunit4ClassRunner is actually calling the constructor of the test cass)