import logging
import os
import time
import uuid

import dotenv
import pymysql

logging.basicConfig(level=logging.DEBUG)

dotenv.load_dotenv()

logger = logging.getLogger('producer')

host = os.getenv('MARIADB_HOST', 'localhost')
port = int(os.getenv('MARIADB_PORT', '3306'))
user = os.getenv('MARIADB_USER', 'root')

logger.info(f'MariaDB host: {host}, port: {port}, user: {user}')

for _ in range(10):
    try:
        with pymysql.connect(host=host,
                            port=port,
                            user=user,
                            password=os.environ['MARIADB_PASSWORD'],
                            ssl=False) as connection:
            with connection.cursor() as cursor:
                cursor.execute('create schema if not exists test')
                cursor.execute('''create table if not exists test.test (
            name varchar(128) primary key not null,
            value varchar(128)             
        )''')
                while True:
                    name = str(uuid.uuid4())
                    cursor.execute('insert into test.test (name, value) values (%s, %s)',
                                (name, str(uuid.uuid4())))
                    connection.commit()
                    time.sleep(0.5)
                    cursor.execute('update test.test set value = %s where name = %s',
                                (str(uuid.uuid4()), name))
                    connection.commit()                                
    except pymysql.err.OperationalError as ex:
        logger.exception(ex)
        time.sleep(5)