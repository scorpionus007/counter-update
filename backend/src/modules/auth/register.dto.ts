import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsNotEmpty, IsString, Length, Matches } from 'class-validator';

export class RegisterDto {
  @ApiProperty({ example: 'Rahul Sharma' })
  @IsString()
  @IsNotEmpty()
  @Length(2, 80)
  name!: string;

  @ApiProperty({ example: '9876543210', description: '10-digit Indian mobile number' })
  @IsString()
  @Matches(/^[6-9]\d{9}$/, { message: 'phone must be a valid 10-digit Indian mobile number' })
  phone!: string;

  @ApiProperty({ example: 'abc123device', description: 'Android device identifier' })
  @IsString()
  @IsNotEmpty()
  androidDeviceId!: string;
}

export class RegisterResponseDto {
  @ApiProperty({ format: 'uuid' })
  userId!: string;

  @ApiProperty({ format: 'uuid' })
  deviceId!: string;

  @ApiProperty()
  accessToken!: string;

  @ApiProperty()
  name!: string;

  @ApiProperty()
  phone!: string;
}
