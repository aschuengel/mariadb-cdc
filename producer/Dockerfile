FROM python:slim

WORKDIR /app/
WORKDIR /app/
COPY Pipfile /app/
COPY Pipfile.lock /app/
RUN pip install pipenv
RUN python -mpipenv requirements > requirements.txt
RUN pip install -r requirements.txt
COPY producer.py /app/
ENTRYPOINT [ "python", "producer.py" ]
 