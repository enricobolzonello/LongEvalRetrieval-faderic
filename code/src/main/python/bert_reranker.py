import sys
from unittest import mock
# One of the imported modules imports the Python-Java bridging module "jnius" (that will no be used)
# Since this script will be called from Java the import would cause a "JVM failed to start" error
mock.patch.dict(sys.modules, {'jnius': mock.Mock()}).start()

# Setting a seed in order to get comparable results across different runs
from transformers import set_seed
set_seed(42)

from pygaggle.rerank.transformer import MonoBERT
from pygaggle.rerank.base import Text, Query

reranker = None

def load_model(model_name: str) -> bool:
    """
    The function loads a MonoBERT model from https://huggingface.co/models with the specified name
    and returns a boolean indicating whether the loading was successful or not.
    
    :param model_name: a string representing the name of the model to be loaded
    :type model_name: str
    :return: a boolean value. It returns True if the model is successfully loaded and False if there is
    an exception raised during the loading process.
    """
    try:
        model = MonoBERT.get_model(model_name)
        global reranker
        reranker = MonoBERT(model=model)
    except:
        return False
    return True

def rerank(query_title: str, doc_id: list, doc_body: list) -> list:
    """
    The function takes a query title, document IDs, and document bodies, and returns a list of scores
    after reranking the documents based on the query.
    
    :param query_title: A string representing the title of the query for which the documents need to be
    reranked
    :type query_title: str
    :param doc_id: A list of document IDs corresponding to the documents in doc_body
    :type doc_id: list
    :param doc_body: A list of strings representing the body of each document
    :type doc_body: list
    :return: a list of scores for each document in the input list, after reranking them based on their
    relevance to the input query.
    """
    passages = []
    for i in range(len(doc_id)):
        passages.append([doc_id[i], doc_body[i]])

    # Initialize reranker arguments
    query = Query(query_title)
    texts = [Text(p[1], {'docid': p[0]}, 0) for p in passages]

    # Rerank and print scores
    reranked = reranker.rerank(query, texts)
    return [doc.score for doc in reranked]
