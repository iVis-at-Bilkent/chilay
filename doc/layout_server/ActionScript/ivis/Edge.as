package ivis
{
	import mx.core.Container;
	
	public class Edge extends Object
	{
		private var _id: String;
		private var _source: Node;
		private var _target: Node;
		
		// Constructor
		public function Edge(id: String = null, source: Node = null, target: Node = null)
		{
			this._id = id;
			this._source = source;
			this._target = target;
		}
		
		//----------------------------------------
		//         Getter & Setter Methods
		//----------------------------------------
		public function get id(): String {
			return _id;
		}
		
		public function set id(value: String): void
		{
			this._id = value;
		}
		
		public function get source(): Node {
			return _source;
		}
		
		public function get target(): Node {
			return _target;
		}
		
		// this function returns the XML equivalence of this edge
		public function asXML(): XML
		{
			return XML('<edge id="' + id + '">' +
				'<sourceNode id="' + source.id + '"/>' +
				'<targetNode id="' + target.id + '"/></edge>');
		}
	}
}