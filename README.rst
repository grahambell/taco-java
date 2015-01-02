Taco Package for Java
=====================

Introduction
------------

Taco is a system for bridging between (mostly scripting) languages.
Its goal is to allow you to call routines written for one language from
another.
It does this by running the second language interpreter in a sub-process,
and passing messages about actions to be performed inside that interpreter.

In principle, to interface scripting languages it might be preferable
to embed the interpreter for one as an extension of the other.
However this might not be convenient or possible,
and would need to be repeated for each combination of languages.
Instead Taco only requires a "client" module and "server" script
for each language, which should be straightforward to install,
and its messages are designed to be generic so that they
can be used between any combination of languages.

For more information about Taco, please see the
`Taco Homepage`_.

.. _`Taco Homepage`: http://grahambell.github.io/taco/

Building
--------

This package can be build using Maven::

    mvn package

License
-------

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

Additional Links
----------------

* `Repository at GitHub <https://github.com/grahambell/taco-java>`_
* `Taco Homepage`_
