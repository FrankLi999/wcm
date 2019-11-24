CREATE TABLE `properties` (
  `CREATED_ON` datetime DEFAULT NULL,
  `APPLICATION` varchar(255) COLLATE utf8_bin NOT NULL,
  `PROFILE` varchar(255) COLLATE utf8_bin NOT NULL,
  `LABEL` varchar(255) COLLATE utf8_bin NOT NULL,
  `PROP_KEY` varchar(255) COLLATE utf8_bin NOT NULL,
  `VALUE` varchar(4096) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`APPLICATION`,`PROFILE`,`LABEL`,`PROP_KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
INSERT INTO properties (CREATED_ON, APPLICATION, PROFILE, LABEL, PROP_KEY, VALUE) VALUES (NULL,'wcm_bpm','dev','latest','test-property','This is my test value');

insert into properties(application, profile, label, prop_key, value)
values('bpm_ui', 'dev', 'master', 'ui.config', '{cipher}AQBaHnv46a6Wv9pHM/3lDLduInVHvIu8c/6LrY4c66RVIUXeDfdgVHdrLT/B7UA/buDXSuozOeSOrJ4qVu2Qz1rq0ZTbI0J9+Xln68Z8zFLHzyuvCmOK5QZL5k/pmAMb0dxMX5S59Aoeqtj0xSoDSNq+UYxZRnce3Iy606Kcxtju88tu1Vz4AEh+QC3KPebDrGgj7GGH6bPh2OyVrqyyuXXP0W2QrpCWtEPr1LVplJeLweNqj8FXYjt/vQHxwmCajDNTdl23B9kSh72WjBWg/x0k1oH1sL5VBPGmxjVXl4c6AjfWziLjtB+Kbil2nmL/Q9Ajd2lLsIjH6BrlqHwO8O5R7XimK+gHPCU9ynyK2DAKP8ymqms0+lHwsFFkzuatlWK5WdexFA0VZ6lHXUEZbEo/gp/aO4O2gWIrHfw8OPT+iEvIv8DJLl+8IjnMVQjCO1OqYl4CS0sAEMrt0c0nDd1FT/FzM8G7nqFgOo7kiKF9lwW07+SJOlY2Oms+6v9radgqi7Ar4fN2DkYfYCUNp0BHkAH8bPcNSCDDguppbMLkTjACFDlksqNrM7J18wvyFXcfED5ctrLMgnRRwC8H4OolwJJP4zN416z7xymqwJuZZEuQFoqtkMCosxVFnJ572QKerEMqAy7GD+7ooWDI0xJFXoHjp876LcHmgOdKBB3dciRUZb2OkcUu7ra87Wc2lfybPJfZ49s5xbwG6iucub81DWmN8zc3XHT3WHk6pbHij3BW3SfZWOuMcsg2HZp4XkL54/6akkcrMias4zUZE2H+MzRD37OFA0gSCjXJm7Ki7yTtaJ3yrdQB6V9PIaXfBZQPVYS8BMk6DuBGuNL2IXXQusxNUvpFyWZFQcnVWwlK9Q4fok9dnn+fwO9YDLl9u5gbbd9iW5wt+fGMQ79LFwyt9LM+aw2ophJ6hcv3sEltCdokpx3F2IDSNkxiiKZxaOONVMZUSrSESNcWnqE0rJ1RmQynzAiNNz1h9ntELthUoQx6iwcFHMY4wYv2WDwMxV++6HQKyZhCR92DwTPSevAjrjyfbZkWqd4P7tcCSSrtX2c0QHXsbIEVR0lWm+URgMwiJv5jSCuzsXDnNRoLHWty2XwRGNEtRLCVeHW9XoPWKZMDWzWxn3WEu6J+pRYju7HJAosIGyoiQNVLvypQXAOIpiKyYEnEJV/DI3Irr12BFtE8TrkqtaUNdP77jGXReF233kXX44axvQJPkcht9+sTR+LgQ+Q/X3kxvVLddzkCUvnewtlJmo8dHMDVNUuj9R+L9l/aAmsUuNoF/nv+JxRVfePnverVt+bP1qV6Xr6UVZNPKb00Kt31zRTsy4APPR17daG+d0+WM8UUS7OYuGZp');

insert into properties(application, profile, label, prop_key, value)
values('authoring_ui', 'dev', 'master', 'ui.config', '{cipher}AQCFp/H+3+Vlsg1CVHuWVXLH+FUL9v4TUolGFUKlrlJxYR5R79faZAWrvNUDF0zaeYTKDxwSn2f76lGJpnLnpLbz2UXyLqWfP9MZdeAtkRZSAQhCI4I+wAGfoH8TbWhfdANKUWr/469SoKGOWG3H4sHFqWsfFJraxS2PC/HL+28gycg4/kbq/OcgE/6ht8QRO14QzJBrtUGF6YQIqVPf8NTilzCfcahOcI9k8+P7Hn4WaAleZjbZcvzkuh9o1W0DnE4ZxLhb8TMojPjPdC2Vyr+CJsd01sOKq9kBMtfaxtFNS8c8aA1oL3ggOo4iL9a0gzvyCCezEEWmDQqRw9ydrrEMS+BAijd9rizzLt+SW5M1hUUZ89g2LWu6SQyWCnWQGTIoCCVTiQmkssHkPrwkMlZxeOYJXdWs9V80c/8J3lj7uhnCTsdsj9M3QSMrCRq+vdbab/Tc+XbceeXDNxa+ST8sqTSCVDvks8CRCOREF8E/xSx+df/GP1LfCioGqwZ7E5gxcIjwa3OP0AUI9nBhXAbQy53pCBJdzrUlEUs4vm38SXOSQ9cAXZxkP0ZAzLmGwQ3O/trFHWBXz76WyIpEWFu54ToUDBPyK1+GS8Tr9spDqZt29mdE+pN1x0Ydruk3EgIqpN+nmp1nKLzJPzqsHCah6DOfn1GAKVFq8uOPfjQP/SMIUpMv0VYBjYIPjMCsJEUmNZKM/UOdKJJAlnLrMSfa8FgoDjgtenfKbThHHo6jZHeXA9yWWpd7MLv6ttxOzyon/B3gFbMiXrUZW4yfSvLWrt0fQplaan3etc74ckiNU4XRDrf5O+55pW8INHGQydlyozLLk6Vk4rJzuGGa2KLOnTPp9mNGLC4vKLnyvrJCRcGFk2HhFP7r5myKpJG+OEER94q9lo4EpxBTZ1xL7QJqGCpufnpucVaAwTDREQZ+UnrtUEZKUL4685/HPmm0F5wUTDOWVn4TPjjbCRMckUPHpW49IEjtmK5RZ556Ibd4Y6h6sRZpqteOzKlZUGYqHZmIOXA840Wg0M9SXteywktjyQxNvvo/TC/ZhW0++U5G1Fw+hbRqeSK7ZIuOOmwC3IZ65eM0eswmsUFKDnsAegGW2b2lOFssb9HwGnVRU5ueaAVx6Wj/Aitj/tjMlXXAXs5VG6qQCTughDzsl6IZxuM3Qk1itBKzzpRVxEKA9VQpF5MNsG2BQGuN9qr1AYD+oF5A5jiMoJ9tjga013E7ne4W78BOT9L0dKf/jk9uJMYA8pSPjessF4Yu4D0dZjtbqzAKTLkWuiA+ZtB1m9xv006BCzCIrc7pPVHuThqWD6bU1unv3NmRy5IH4jrAfw9iqu38ECGjNyEh9A8p9SxtuBTP');



update properties set value = '{cipher}AQBaHnv46a6Wv9pHM/3lDLduInVHvIu8c/6LrY4c66RVIUXeDfdgVHdrLT/B7UA/buDXSuozOeSOrJ4qVu2Qz1rq0ZTbI0J9+Xln68Z8zFLHzyuvCmOK5QZL5k/pmAMb0dxMX5S59Aoeqtj0xSoDSNq+UYxZRnce3Iy606Kcxtju88tu1Vz4AEh+QC3KPebDrGgj7GGH6bPh2OyVrqyyuXXP0W2QrpCWtEPr1LVplJeLweNqj8FXYjt/vQHxwmCajDNTdl23B9kSh72WjBWg/x0k1oH1sL5VBPGmxjVXl4c6AjfWziLjtB+Kbil2nmL/Q9Ajd2lLsIjH6BrlqHwO8O5R7XimK+gHPCU9ynyK2DAKP8ymqms0+lHwsFFkzuatlWK5WdexFA0VZ6lHXUEZbEo/gp/aO4O2gWIrHfw8OPT+iEvIv8DJLl+8IjnMVQjCO1OqYl4CS0sAEMrt0c0nDd1FT/FzM8G7nqFgOo7kiKF9lwW07+SJOlY2Oms+6v9radgqi7Ar4fN2DkYfYCUNp0BHkAH8bPcNSCDDguppbMLkTjACFDlksqNrM7J18wvyFXcfED5ctrLMgnRRwC8H4OolwJJP4zN416z7xymqwJuZZEuQFoqtkMCosxVFnJ572QKerEMqAy7GD+7ooWDI0xJFXoHjp876LcHmgOdKBB3dciRUZb2OkcUu7ra87Wc2lfybPJfZ49s5xbwG6iucub81DWmN8zc3XHT3WHk6pbHij3BW3SfZWOuMcsg2HZp4XkL54/6akkcrMias4zUZE2H+MzRD37OFA0gSCjXJm7Ki7yTtaJ3yrdQB6V9PIaXfBZQPVYS8BMk6DuBGuNL2IXXQusxNUvpFyWZFQcnVWwlK9Q4fok9dnn+fwO9YDLl9u5gbbd9iW5wt+fGMQ79LFwyt9LM+aw2ophJ6hcv3sEltCdokpx3F2IDSNkxiiKZxaOONVMZUSrSESNcWnqE0rJ1RmQynzAiNNz1h9ntELthUoQx6iwcFHMY4wYv2WDwMxV++6HQKyZhCR92DwTPSevAjrjyfbZkWqd4P7tcCSSrtX2c0QHXsbIEVR0lWm+URgMwiJv5jSCuzsXDnNRoLHWty2XwRGNEtRLCVeHW9XoPWKZMDWzWxn3WEu6J+pRYju7HJAosIGyoiQNVLvypQXAOIpiKyYEnEJV/DI3Irr12BFtE8TrkqtaUNdP77jGXReF233kXX44axvQJPkcht9+sTR+LgQ+Q/X3kxvVLddzkCUvnewtlJmo8dHMDVNUuj9R+L9l/aAmsUuNoF/nv+JxRVfePnverVt+bP1qV6Xr6UVZNPKb00Kt31zRTsy4APPR17daG+d0+WM8UUS7OYuGZp' where application='bpm_ui';