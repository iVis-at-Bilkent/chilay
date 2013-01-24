package ivis
{
	import flash.geom.*;
	
	import mx.core.Container;
	
	public class Graph
	{
		public var _nodes: Array = new Array; // nodes at the root level
		public var _edges: Array = new Array; // all edges (including inter-graph ones)
		public var _allNodes: Array = new Array; // all nodes
		
		// general options
		private var _generalOptions:Object = {
			quality: DEFAULT_QUALITY,
			incremental: false,
			animateOnLayout: true
		}
		
		// CoSE options
		private var _CoSEOptions:Object = {
			idealEdgeLength: DEFAULT_EDGE_LENGTH,
			springStrength: DEFAULT_SPRING_STRENGTH,
			repulsionStrength: DEFAULT_REPULSION_STRENGTH,
			gravityStrength: DEFAULT_GRAVITY_STRENGTH,
			compoundGravityStrength: DEFAULT_COMPOUND_GRAVITY_STRENGTH
		}
			
		private static var _instance: Graph;
		
		// General options consts		
		public static const PROOF_QUALITY:int = 0;
		public static const DEFAULT_QUALITY:int = 1;
		public static const DRAFT_QUALITY:int = 2;
		// CoSE options consts
		public static const DEFAULT_EDGE_LENGTH:uint = 40;
		public static const DEFAULT_SPRING_STRENGTH:Number = 50;
		public static const DEFAULT_REPULSION_STRENGTH:Number = 50;
		public static const DEFAULT_GRAVITY_STRENGTH:Number = 50;
		public static const DEFAULT_COMPOUND_GRAVITY_STRENGTH:Number = 50;
		
		// Constructor
		public function Graph()
		{
			_instance = this;
		}
		
		//----------------------------------------
		//         Getter & Setter Methods
		//----------------------------------------
		public function get generalOptions(): Object
		{
			return _generalOptions
		}
		
		public function set generalOptions(value: Object): void
		{
			this._generalOptions = value;
		}
		
		public function get CoSEOptions(): Object
		{
			return this._CoSEOptions
		}
		
		public function set CoSEOptions(value: Object): void
		{
			this._CoSEOptions = value
		}
		
		public function get bounds(): Object
		{
			var pts: Array = Utils.cloneArray(this._nodes)
			
			var b:* = Utils.boundingRect(pts)
			
			return { left: b.left, top: b.top, width: b.width, height: b.height }			
		}
		
		public static function getInstance(): Graph
		{
			return _instance;
		}
		
		// this function returns the XML equivalence of this graph
		public function toXML(): XML
		{
			var result:String = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><view>'
			
			for each(var n: Node in _nodes)
			{
				if (n.parent == null)
				{
					result += n.asXML();
				}
			}
			
			for each(var e: Edge in _edges)
			{
				result += e.asXML()
			}
			
			result += "</view>"
			
			return XML(result);
		}
		
		// this function constructs a graph model from given XML
		public function fromXML(xml: XML): void
		{
			this.reset();

			for each(var node: XML in xml.node)
			{
				this._nodes.push(this.nodeFromXML(node));
			}
			
			for each(var edge: XML in xml..edge) 
			{
				var fromId: String = edge.sourceNode.@id;
				var toId: String = edge.targetNode.@id;
				
				if((fromId != null) && (toId != null)) 
				{
					this.addEdge(edge.@id, fromId, toId);
				}
			}
		}
		
		// this function constructs a node from given XML
		private function nodeFromXML(node: XML): Node
		{
			var isCompound: Boolean = (node.children.length() > 0);
			var n: Node = isCompound ? new CompoundNode(node.@id): new Node(node.@id);
			
			n.x = node.bounds.@x;
			n.y = node.bounds.@y;
			n.width = node.bounds.@width;
			n.height = node.bounds.@height;
			n.label = node.@label;
			
			if (isCompound)
			{
				var childNode: XML;
				
				for each(childNode in node.children.node)
				{
					(n as CompoundNode).addNode(this.nodeFromXML(childNode));
				}
			}
			
			return n;
		}
				
		// this function adds an edge to this graph
		public function addEdge(edgeId: String, fromId: String, toId: String): Edge
		{
			var source: Node = this.nodeFromId(fromId);
			var target: Node = this.nodeFromId(toId);
			
			var e: Edge = new Edge(edgeId, source, target)
			
			_edges.push(e);
			
			return e;
		}
		
		// this function finds the node which has the given id
		// it searches for all nodes
		public function nodeFromId(id: String): Node
		{
			this.prepareAllNodes();
			
			for each(var node: Node in _allNodes)
			{
				if(node.id == id)
				{
					return node;
				}
			}
			
			return null;
		}
		
		// allNodes instance variable is constructed
		private function prepareAllNodes(): void
		{
			if (this._allNodes.length == 0)
			{
				for each(var node: Node in _nodes)
				{
					this._allNodes.push(node);
					
					if (node.isCompound())
					{
						this.prepareAllNodesRecursive((node as CompoundNode));
					}
				}
			}
		}
		
		private function prepareAllNodesRecursive(compound: CompoundNode): void
		{
			for each(var child:Node in compound.nodes)
			{
				this._allNodes.push(child);
				
				if (child.isCompound())
				{
					this.prepareAllNodesRecursive((child as CompoundNode));
				}
			}
		}
		
		private function reset(): void 
		{
			var i: int = 0
			for(; i < _nodes.length; ++i)
			{
				delete _nodes[i];
			}
			
			for(i = 0; i < _edges.length; ++i)
			{
				delete _edges[i];
			}
			
			for(i = 0; i < _allNodes.length; ++i)
			{
				delete _allNodes[i];
			}
			
			_edges = new Array;
			_nodes = new Array;
			_allNodes = new Array;
		}
	}
}