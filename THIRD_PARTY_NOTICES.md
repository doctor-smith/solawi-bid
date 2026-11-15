# Third-party notices

## MariaDB Connector/J

The backend distribution uses MariaDB Connector/J 3.5.11, copyright
MariaDB Corporation Ab and contributors, under the GNU Lesser General Public
License, version 2.1 or (at your option) any later version (LGPL-2.1-or-later).

Project: https://github.com/mariadb-corporation/mariadb-connector-j

Corresponding source for the distributed version:
https://github.com/mariadb-corporation/mariadb-connector-j/tree/3.5.11

The unmodified driver is distributed as a separate file under `/app/lib` in
the backend container. It can be replaced with an interface-compatible version
without rebuilding `server.jar`. The complete license text is included at
`third-party/licenses/MariaDB-Connector-J-LGPL-2.1.txt` and in the container at
`/app/licenses/MariaDB-Connector-J-LGPL-2.1.txt`.

This notice does not change the license of the Solawi Bid application itself.
