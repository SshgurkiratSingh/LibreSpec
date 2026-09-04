import json
import hashlib
import time
import boto3
from boto3.dynamodb.conditions import Key

dynamodb = boto3.resource('dynamodb')
table = dynamodb.Table('BiochemicalForensicLogs')

def lambda_handler(event, context):
    http_method = event.get('requestContext', {}).get('http', {}).get('method')
    
    if http_method == 'POST':
        body = json.loads(event.get('body', '{}'))
        
        # Reconstruct the Android app's hash payload
        baseline_str = ",".join(map(str, body['baseline_vector']))
        plateau_str = ",".join(map(str, body['plateau_vector']))
        payload_string = f"{baseline_str}|{plateau_str}|{body['timestamp']}"
        
        calculated_hash = hashlib.sha256(payload_string.encode('utf-8')).hexdigest()
        
        if calculated_hash != body['app_generated_hash']:
            return {'statusCode': 403, 'body': json.dumps({'error': 'Transit integrity failure.'})}
            
        item = {
            'test_id': body['test_id'],
            'timestamp': body['timestamp'],
            'server_ingest_time': int(time.time()),
            'predicted_class': body['predicted_class'],
            'confidence_score': body['confidence_score'],
            'baseline_vector': body['baseline_vector'],
            'plateau_vector': body['plateau_vector'],
            'cryptographic_seal': body['app_generated_hash'],
            'latitude': body.get('latitude', 0.0),
            'longitude': body.get('longitude', 0.0)
        }
        
        table.put_item(Item=item)
        return {'statusCode': 201, 'body': json.dumps({'status': 'Ingested'})}
        
    elif http_method == 'GET':
        query_params = event.get('queryStringParameters', {})
        if 'select_best' in query_params:
            response = table.query(
                IndexName='ConfidenceIndex',
                KeyConditionExpression=Key('predicted_class').eq(query_params['substance']),
                ScanIndexForward=False,
                Limit=int(query_params.get('limit', 5))
            )
            return {'statusCode': 200, 'body': json.dumps(response.get('Items', []))}
