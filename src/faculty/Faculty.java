package faculty;

public class Faculty {

	
		private int facultyId;
		private String facultyName , designation, dept;
		
		public Faculty(int id,String name,String des,String dept){
			facultyId = id;
			facultyName = name;
			designation = des;
			this.dept = dept; 
		}
		public int getId() {
			return facultyId;			
		}
		public String getname() {
			return facultyName;			
		}
		public String getDes() {
			return designation;			
		}
		public String getdept() {
			return dept;			
		}
		
		
		
		public void setname(String fn) {
			facultyName = fn;			
		}
		public void setDes(String fd) {
			designation = fd;			
		}
		public void setdept(String de) {
			 dept = de;			
		}
	

}
